#Requires -Version 5.1
<#
.SYNOPSIS
    测试阶段一键构建 + 部署（前端 hcs.war + 后端 api.war → 同一 Tomcat）。

.DESCRIPTION
    把 docs/../frontend/docs/build.md 里的多步手工流程固化成一条命令。
    当前「前后端 war 同 Tomcat」是临时/过渡形态，后期部署方式调整时改本脚本即可。

.EXAMPLE
    .\deploy-test.ps1                  # 全量：构建前后端 + 部署 + 跟踪日志
    .\deploy-test.ps1 -Part Front      # 只重新构建并部署前端（后端不动）
    .\deploy-test.ps1 -Part Back       # 只重新构建并部署后端
    .\deploy-test.ps1 -NoBuild         # 跳过构建，直接部署已有的 war
    .\deploy-test.ps1 -NoTail          # 部署完不跟踪日志
#>
param(
    [ValidateSet('All', 'Front', 'Back')]
    [string]$Part = 'All',

    [string]$Tomcat = 'E:\software\tomcat11',

    # 跳过构建，直接用现有的 hcs.war / backend\target\api.war 部署
    [switch]$NoBuild,

    # 部署完成后不跟踪 catalina 日志
    [switch]$NoTail,

    # 后端构建不跳过测试（默认 -DskipTests，src/test 当前为空）
    [switch]$RunTests,

    # Tomcat 超时未退出时强制结束 java 进程（测试机可用，会杀掉本机全部 java）
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

$root     = 'f:\hb\page'
$frontDir = Join-Path $root 'frontend'
$backDir  = Join-Path $root 'backend'
$frontWar = Join-Path $frontDir 'hcs.war'
$backWar  = Join-Path $backDir  'target\api.war'
$webapps  = Join-Path $Tomcat 'webapps'
$work     = Join-Path $Tomcat 'work\Catalina\localhost'

function Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Ok($msg)   { Write-Host "  [OK] $msg" -ForegroundColor Green }
function Warn($msg) { Write-Host "  [!!] $msg" -ForegroundColor Yellow }
function Die($msg)  { throw $msg }

if (-not (Test-Path $webapps)) { Die "Tomcat webapps 目录不存在：$webapps（用 -Tomcat 指定正确路径）" }

# 按 -Part 决定本次要处理的 app（顺序：先后端后前端）
$apps = switch ($Part) {
    'All'   { @('api', 'hcs') }
    'Back'  { @('api') }
    'Front' { @('hcs') }
}

# ---------------------------------------------------------------- 构建
if (-not $NoBuild) {
    if ($apps -contains 'api') {
        Step '构建后端 api.war'
        if (-not (Test-Path (Join-Path $backDir 'mvnw.cmd'))) { Die "未找到 $backDir\mvnw.cmd" }
        Push-Location $backDir
        try {
            $mvnArgs = @('package', '-q')
            if (-not $RunTests) { $mvnArgs += '-DskipTests' }
            & .\mvnw.cmd @mvnArgs
            if ($LASTEXITCODE -ne 0) { Die '后端构建失败（见上方 Maven 输出）' }
        } finally { Pop-Location }
        if (-not (Test-Path $backWar)) { Die "未找到产物：$backWar" }
        Ok ("api.war  {0:N1} MB" -f ((Get-Item $backWar).Length / 1MB))
    }

    if ($apps -contains 'hcs') {
        Step '构建前端 hcs.war'
        Push-Location $frontDir
        try {
            & npm run build:war
            if ($LASTEXITCODE -ne 0) { Die '前端构建失败（见上方 vite/vue-tsc 输出）' }
        } finally { Pop-Location }
        if (-not (Test-Path $frontWar)) { Die "未找到产物：$frontWar" }
        Ok ("hcs.war  {0:N1} MB" -f ((Get-Item $frontWar).Length / 1MB))
    }
}

# ---------------------------------------------------------------- 产物自检
if ($apps -contains 'hcs') {
    if (-not (Test-Path $frontWar)) { Die "前端 war 不存在：$frontWar（先构建或去掉 -NoBuild）" }
    Step '自检 hcs.war'
    $entries = & tar -tf $frontWar 2>$null
    if (-not $entries) {
        Warn 'tar 不可用，跳过 war 条目自检'
    } else {
        if (-not ($entries -match '^assets/$'))  { Die 'hcs.war 缺少 assets/ 目录条目，Tomcat 11 解包会 FileNotFoundException' }
        if (-not ($entries -match '^WEB-INF/web\.xml$')) { Die 'hcs.war 缺少 WEB-INF/web.xml，history 子路由刷新会 404' }
        Ok '目录条目与 404 回退齐全'
    }
}
if ($apps -contains 'api' -and -not (Test-Path $backWar)) {
    Die "后端 war 不存在：$backWar（先构建或去掉 -NoBuild）"
}

# ---------------------------------------------------------------- 停止 Tomcat
Step '停止 Tomcat'
$shutdown = Join-Path $Tomcat 'bin\shutdown.bat'
if (Test-Path $shutdown) { & cmd /c "`"$shutdown`"" | Out-Null } else { Warn "未找到 $shutdown" }

$deadline = (Get-Date).AddSeconds(30)
while ((Get-Process -Name java -ErrorAction SilentlyContinue) -and (Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 1
}
if (Get-Process -Name java -ErrorAction SilentlyContinue) {
    if ($Force) {
        Warn 'Tomcat 未自行退出，强制结束 java 进程'
        Stop-Process -Name java -Force
        Start-Sleep -Seconds 2
    } else {
        Die 'Tomcat 30 秒内未退出。请手动确认后重跑，或加 -Force 强制结束 java 进程（会杀掉本机全部 java）'
    }
}
Ok 'Tomcat 已停止'

# ---------------------------------------------------------------- 清理残留
Step '清理旧部署残留'
foreach ($app in $apps) {
    Remove-Item -Recurse -Force (Join-Path $webapps $app) -ErrorAction SilentlyContinue
    Remove-Item -Recurse -Force (Join-Path $work $app)    -ErrorAction SilentlyContinue
}
# war 总是覆盖拷贝，无需删除；但半途失败留下的目录必须清掉，否则 Tomcat 不会重新解压
Ok ("已清理：{0}" -f ($apps -join ', '))

# ---------------------------------------------------------------- 拷贝
Step '拷贝新包'
if ($apps -contains 'api') { Copy-Item $backWar  (Join-Path $webapps 'api.war') -Force; Ok 'api.war' }
if ($apps -contains 'hcs') { Copy-Item $frontWar (Join-Path $webapps 'hcs.war') -Force; Ok 'hcs.war' }

# ---------------------------------------------------------------- 启动
Step '启动 Tomcat'
Start-Process -FilePath (Join-Path $Tomcat 'bin\startup.bat') -WorkingDirectory (Join-Path $Tomcat 'bin')
Ok "已启动，前端 http://localhost:8080/hcs/  后端 http://localhost:8080/api/"

if ($NoTail) { exit 0 }

$log = Get-ChildItem (Join-Path $Tomcat 'logs') -Filter 'catalina*.log' -ErrorAction SilentlyContinue |
       Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $log) { Warn '未找到 catalina 日志'; exit 0 }

Write-Host "`n跟踪日志（Ctrl+C 退出）：$($log.FullName)" -ForegroundColor Cyan
Write-Host '  成功特征：Started ApiApplication / Initializing ProtocolHandler ["http-nio-8080"]' -ForegroundColor DarkGray
Write-Host '  失败特征：HikariPool-1 - Exception during pool initialization / FileNotFoundException: webapps\hcs\assets' -ForegroundColor DarkGray
Get-Content $log.FullName -Wait -Tail 40
