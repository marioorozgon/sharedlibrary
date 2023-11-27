@NonCPS
def constructPrometheusMetrics(Map metrics, String hash) {
    metricData = ''

    // MÃ©tricas de testsuites
    testsuitesValue = (metrics.testsuites.totalFailures > 0 || metrics.testsuites.totalErrors > 0) ? 1 : 0
    metricData += "katalon_testsuites_info{name=\"${metrics.testsuites.name}\", tests=\"${metrics.testsuites.totalTests}\", failures=\"${metrics.testsuites.totalFailures}\", errors=\"${metrics.testsuites.totalErrors}\", time=\"${metrics.testsuites.totalTime}\", hash=\"${hash}\", timestamp_execute=\"${env.PIPELINE_TIMESTAMP}\"} ${testsuitesValue}\n"

    // MÃ©tricas de testsuite
    metrics.testsuite.each { ts ->
        estsuiteValue = (ts.failures > 0 || ts.errors > 0) ? 1 : 0
        metricData += "katalon_testsuite_info{name=\"${ts.name}\",tests=\"${ts.tests}\",failures=\"${ts.failures}\",errors=\"${ts.errors}\",time=\"${ts.time}\",skipped=\"${ts.skipped}\",timestamp=\"${ts.timestamp}\", hash=\"${hash}\"} ${testsuiteValue}\n"
    }

    // MÃ©tricas de testcase con etiqueta de testsuite
    metrics.testsuite.each { ts ->
        tsName = ts.name
        metrics.testcase[tsName]?.each { tc -> // testcase del testsuite actual
                testcaseValue = (tc.status == 'PASSED') ? 0 : 1
                testcaseMetric = "katalon_testcase_info{testsuite=\"${ts.name}\", name=\"${tc.name}\", time=\"${tc.time}\", status=\"${tc.status}\", hash=\"${hash}\""


                if (testcaseValue == 0) {
                    testcaseMetric += ", result=\"OK\"} ${testcaseValue}\n"
                } else {
                    String detailType = tc.status == 'ERROR' ? 'error' : 'failure'
                    testcaseMetric += ", ${detailType}_type=\"${tc[detailType + 'Type']}\", ${detailType}_message=\"${tc[detailType + 'Message'].replaceAll('"', '\\"')}\"} ${testcaseValue}\n"
                }

                metricData += testcaseMetric
            }
        }
    
    return metricData
}