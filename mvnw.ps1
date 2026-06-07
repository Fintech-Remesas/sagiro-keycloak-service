$ErrorActionPreference = "Stop"

# Define directories
$BaseDir = $PSScriptRoot
$WrapperDir = Join-Path $BaseDir ".mvn\wrapper"
$PropertiesFile = Join-Path $WrapperDir "maven-wrapper.properties"

if (-not (Test-Path $PropertiesFile)) {
    Write-Error "Missing $PropertiesFile"
    Exit 1
}

# Read properties
$Properties = @{}
Get-Content $PropertiesFile | Where-Object { $_ -match '=' -and -not $_.StartsWith("#") } | ForEach-Object {
    $parts = $_.Split('=', 2)
    $Properties[$parts[0].Trim()] = $parts[1].Trim()
}

$DistributionUrl = $Properties["distributionUrl"]
if (-not $DistributionUrl) {
    Write-Error "distributionUrl not found in $PropertiesFile"
    Exit 1
}

# Determine Maven Version and Archive Name
$ArchiveName = Split-Path $DistributionUrl -Leaf
if ($DistributionUrl -match 'apache-maven/([^/]+)/') {
    $MavenVersion = $Matches[1]
} else {
    # Fallback/extract from filename
    if ($ArchiveName -match 'apache-maven-([0-9.]+)-bin') {
        $MavenVersion = $Matches[1]
    } else {
        $MavenVersion = "3.9.9" # Default fallback
    }
}

$MavenHome = Join-Path $WrapperDir "apache-maven-$MavenVersion"
$ArchivePath = Join-Path $WrapperDir $ArchiveName
$MvnCmd = Join-Path $MavenHome "bin\mvn.cmd"

# Download and Extract Maven if not present
if (-not (Test-Path $MvnCmd)) {
    if (-not (Test-Path $ArchivePath)) {
        Write-Host "Downloading Maven from $DistributionUrl..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $DistributionUrl -OutFile $ArchivePath -UseBasicParsing
    }

    Write-Host "Extracting Maven..."
    if (Test-Path $MavenHome) {
        Remove-Item -Path $MavenHome -Recurse -Force
    }

    if ($ArchiveName.EndsWith(".tar.gz")) {
        # Modern Windows has tar.exe built-in
        & tar -xzf $ArchivePath -C $WrapperDir
    } else {
        Expand-Archive -Path $ArchivePath -DestinationPath $WrapperDir -Force
    }
}

if (-not (Test-Path $MvnCmd)) {
    Write-Error "Maven executable not found at $MvnCmd"
    Exit 1
}

# Run Maven with all passed arguments
& $MvnCmd $args
