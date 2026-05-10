# High-Performance Spring Persistence Demo

This repository contains the demo for the High-Performance Spring Persistence talk that I first presented at DevTalks Cluj-Napoca 2025.

## JMH Banchmark Results

````
Result "com.vladmihalcea.spring.demo.benchmark.BlazePersistenceBenchmarkTest.findWithCommentsAndTagsByIds":
  23.976 ┬▒(99.9%) 1.787 ms/op [Average]
  (min, avg, max) = (21.654, 23.976, 40.560), stdev = 3.610
  CI (99.9%): [22.188, 25.763] (assumes normal distribution)
  
Result "com.vladmihalcea.spring.demo.benchmark.JdbcBenchmarkTest.findWithCommentsAndTagsByIds":
  13.479 ┬▒(99.9%) 0.376 ms/op [Average]
  (min, avg, max) = (12.786, 13.479, 15.949), stdev = 0.760
  CI (99.9%): [13.103, 13.856] (assumes normal distribution)
  
Result "com.vladmihalcea.spring.demo.benchmark.JdbcTemplateBenchmarkTest.findWithCommentsAndTagsByIds":
  474.419 ┬▒(99.9%) 14.802 ms/op [Average]
  (min, avg, max) = (444.814, 474.419, 575.581), stdev = 29.900
  CI (99.9%): [459.617, 489.221] (assumes normal distribution)
  
Result "com.vladmihalcea.spring.demo.benchmark.JooqBenchmarkTest.findWithCommentsAndTagsByIds":
  52.967 ┬▒(99.9%) 2.100 ms/op [Average]
  (min, avg, max) = (48.563, 52.967, 66.428), stdev = 4.241
  CI (99.9%): [50.867, 55.066] (assumes normal distribution)
  
Result "com.vladmihalcea.spring.demo.benchmark.JooqMultiQueryBenchmarkTest.findWithCommentsAndTagsByIds":
  19.028 ┬▒(99.9%) 0.592 ms/op [Average]
  (min, avg, max) = (17.597, 19.028, 23.390), stdev = 1.197
  CI (99.9%): [18.435, 19.620] (assumes normal distribution)

Result "com.vladmihalcea.spring.demo.benchmark.JpaBenchmarkTest.findWithCommentsAndTagsByIds":
  23.002 ┬▒(99.9%) 0.462 ms/op [Average]
  (min, avg, max) = (21.564, 23.002, 25.795), stdev = 0.934
  CI (99.9%): [22.539, 23.464] (assumes normal distribution)
  
Result "com.vladmihalcea.spring.demo.benchmark.SpringDataJdbcNPlus1BenchmarkTest.findWithCommentsAndTagsByIds":
  350.649 ┬▒(99.9%) 9.478 ms/op [Average]
  (min, avg, max) = (329.162, 350.649, 417.297), stdev = 19.146
  CI (99.9%): [341.171, 360.126] (assumes normal distribution)
  
Result "com.vladmihalcea.spring.demo.benchmark.SpringDataJdbcSingleQueryLoadingBenchmarkTest.findWithCommentsAndTagsByIds":
  21.617 ┬▒(99.9%) 0.760 ms/op [Average]
  (min, avg, max) = (20.066, 21.617, 25.987), stdev = 1.535
  CI (99.9%): [20.857, 22.376] (assumes normal distribution)
````