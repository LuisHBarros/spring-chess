# Build Spring Chess microservice images for Docker Swarm deployment.
# Requires Maven and Docker.

$ErrorActionPreference = "Stop"

$services = @(
    @{ Name = "auth"; Port = 8080 },
    @{ Name = "social"; Port = 8081 },
    @{ Name = "chat"; Port = 8082 },
    @{ Name = "game"; Port = 8083 }
)

foreach ($svc in $services) {
    $dir = $svc.Name
    Write-Host "Building $dir ..." -ForegroundColor Cyan
    Set-Location $dir
    try {
        mvn -B "-Dmaven.test.skip=true" package
        docker build -t "chess-$($svc.Name):latest" .
    } finally {
        Set-Location ..
    }
}

Write-Host "Images built. Deploy with: docker stack deploy -c docker-compose.swarm.yml spring-chess" -ForegroundColor Green
