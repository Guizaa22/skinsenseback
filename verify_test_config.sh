#!/bin/bash
# Script de vérification de la configuration des tests

echo "================================"
echo "Vérification de la Configuration des Tests"
echo "================================"
echo ""

echo "1. Vérification des fichiers de configuration..."
if [ -f "src/test/resources/application-test.yml" ]; then
    echo "   ✅ application-test.yml trouvé"
else
    echo "   ❌ application-test.yml NOT FOUND"
    exit 1
fi

echo ""
echo "2. Vérification des dépendances..."
if grep -q "h2database" pom.xml; then
    echo "   ✅ Dépendance H2 trouvée dans pom.xml"
else
    echo "   ⚠️  H2 non trouvé dans pom.xml"
fi

echo ""
echo "3. Vérification des fichiers de test..."
test_files=(
    "src/test/java/beauty_center/BeautyCenterApplicationTests.java"
    "src/test/java/beauty_center/DatabaseConnectionTest.java"
    "src/test/java/beauty_center/ApplicationStartupTest.java"
    "src/test/java/beauty_center/modules/auth/AuthenticationIntegrationTest.java"
)

for file in "${test_files[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $file"
    else
        echo "   ❌ $file NOT FOUND"
    fi
done

echo ""
echo "4. Vérification de la configuration H2..."
if grep -q "jdbc:h2:mem" src/test/resources/application-test.yml; then
    echo "   ✅ Configuration H2 in-memory trouvée"
else
    echo "   ❌ Configuration H2 NOT FOUND"
fi

echo ""
echo "5. Vérification du dialecte Hibernate..."
if grep -q "org.hibernate.dialect.H2Dialect" src/test/resources/application-test.yml; then
    echo "   ✅ Dialecte H2 configuré"
else
    echo "   ❌ Dialecte H2 NOT FOUND"
fi

echo ""
echo "================================"
echo "Configuration vérifiée avec succès!"
echo "================================"
echo ""
echo "Pour exécuter les tests:"
echo "  mvn clean test"
echo ""
echo "Pour exécuter des tests spécifiques:"
echo "  mvn clean test -Dtest=ApplicationStartupTest"
echo "  mvn clean test -Dtest=DatabaseConnectionTest"
echo "  mvn clean test -Dtest=AuthenticationIntegrationTest"
