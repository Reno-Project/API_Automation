@echo off
echo ========================================
echo Running API Tests with Fail-Fast Mode
echo ========================================
echo.

echo Running Contractor Onboarding Tests...
mvn test -Dtest=Contractor_Onboarding_Flow -DsuiteXmlFile=testng-contractor.xml

echo.
echo ========================================
echo Test execution completed
echo ========================================
pause
