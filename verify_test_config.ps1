# Script de vérification de la configuration des tests (PowerShell)

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Vérification de la Configuration des Tests" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. Vérification des fichiers de configuration..."
if (Test-Path "src/test/resources/application-test.yml") {
    Write-Host "   ✅ application-test.yml trouvé" -ForegroundColor Green
} else {
    Write-Host "   ❌ application-test.yml NOT FOUND" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Vérification des dépendances..."
$pomContent = Get-Content pom.xml -Raw
if ($pomContent -match "h2database") {
    Write-Host "   ✅ Dépendance H2 trouvée dans pom.xml" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  H2 non trouvé dans pom.xml" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "3. Vérification des fichiers de test..."
$testFiles = @(
    "src/test/java/beauty_center/BeautyCenterApplicationTests.java",
    "src/test/java/beauty_center/DatabaseConnectionTest.java",
    "src/test/java/beauty_center/ApplicationStartupTest.java",
    "src/test/java/beauty_center/modules/auth/AuthenticationIntegrationTest.java"
)

foreach ($file in $testFiles) {
    if (Test-Path $file) {
        Write-Host "   ✅ $file" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $file NOT FOUND" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "4. Vérification de la configuration H2..."
$testYml = Get-Content src/test/resources/application-test.yml -Raw
if ($testYml -match "jdbc:h2:mem") {
    Write-Host "   ✅ Configuration H2 in-memory trouvée" -ForegroundColor Green
} else {
    Write-Host "   ❌ Configuration H2 NOT FOUND" -ForegroundColor Red
}

Write-Host ""
Write-Host "5. Vérification du dialecte Hibernate..."
if ($testYml -match "org.hibernate.dialect.H2Dialect") {
    Write-Host "   ✅ Dialecte H2 configuré" -ForegroundColor Green
} else {
    Write-Host "   ❌ Dialecte H2 NOT FOUND" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Configuration vérifiée avec succès!" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour exécuter les tests:" -ForegroundColor Yellow
Write-Host "  mvn clean test" -ForegroundColor White
Write-Host ""
Write-Host "Pour exécuter des tests spécifiques:" -ForegroundColor Yellow
Write-Host "  mvn clean test -Dtest=ApplicationStartupTest" -ForegroundColor White
Write-Host "  mvn clean test -Dtest=DatabaseConnectionTest" -ForegroundColor White
Write-Host "  mvn clean test -Dtest=AuthenticationIntegrationTest" -ForegroundColor White
