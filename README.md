# openHAB Winthings binding

# Prerequisites:
  - To have an account in withings.com
  - Java 8 (zulu8.46.0.19-ca-jdk8.0.252) and Apache Maven 3.5.2 or later
  - installed 'openhab-2.5.4' see https://www.openhab.org/download/

# 1. Download and build the projects (using: mvn install or eclipse neon or later) in the following order:
  - org-osgi-bom (pom - BOM project)
  - org-pekoo-bundles (pom - parent bundles project)
  
# 2. Place the builded jars 'org.openhab.binding.withings-2.4.0.jar' and 'org-pekoo-proxy-1.0.0.jar' into your 'OPENHAB_HOME\addons' directory

# 3. create file 'withings.items' into 'OPENHAB_HOME\conf\items' with the following content:
Number Weight     "Weight  [%.1f kg]"     { withings = "weight" }
Number FatRatio   "FatRatio [%.1f %%]"    { withings = "fat_ratio" }
Number HeartPulse "HeartPulse [%d bpm]"   { withings = "heart_pulse" }
Number PHeight    "Height  [%d in]"       { withings = "thomas:height" }

# 4. start 'OPENHAB_HOME\start.bat' (Windows in administrator mode) and wait the Administrator Karaf console to load
# 5. in the Administrator Karaf console type 'bundle:list'. The printed list should end like this: 
2xx │ Active │  80 │ 1.0.0                   │ org-pekoo-proxy
2xx │ Active │  80 │ 2.4.0                   │ openHAB Withings Binding

# 6. In your browser type 'http://localhost:8080/paperui/'.
Navigate to 'Configuration->Bindings' and check that 'openHAB Withings Binding' is there.
Check 'Configuration->Items' to verify 'withings.items' file created earlier.

# 7. Check the 'OPENHAB_HOME\userdata\logs\openhab.log' for:
2020-05-03 13:57:03.830 [INFO ] [el.core.internal.ModelRepositoryImpl] - Loading model 'withings.items'

# 8. Open another window in your browser using 'http://localhost:8190/withings-spring/register' (notice the port is 8190):
 - Regiser new user name (e.g. thomas). On success you will be redirected to 'Authorize OpenHab Withings User' proxy page. 
 - Enter the user name previously registered again and submit. The proxy will redirect you to 'withings.com'
 - Authorize the partner application in wintings page. (It's all about mapping the new user in openHab with your withings account)
 
# 9. Check Administrator Karaf console for:
WithingsApiClient->about to call proxy:http://localhost:8190/withings-spring/measure/thomas/getmeas
#########################################################################################
Withings authentication SUCCEEDED. Binding is now ready to work.
#########################################################################################

# 10. Check the 'OPENHAB_HOME\userdata\logs\openhab.log' for:
2020-05-03 13:58:26.792 [INFO ] [s.internal.api.WithingsAuthenticator] - Adding account for thomas
2020-05-03 13:58:32.026 [INFO ] [b.core.service.AbstractActiveService] - Withings Refresh Service has been started
2020-05-03 13:58:32.030 [WARN ] [ithings.internal.api.WithingsAccount] - Saved Withings account to file 'C:\OpenHab2\OPENHA~1.4\userdata\.\configurations\services\withings.cfg'.

# 11. Each hour the Withings Binding will poll to check for new items. Probably you will see (openhab.log): 
2020-05-03 20:58:37.502 [WARN ] [ng.withings.internal.WithingsBinding] - No new measures found since the last update.
