Use this module to scan the available GitHub search results for top N most popular public java classes.
If there are more than N classes with the same top usage frequency, all of them will be reported.

External libraries: <br>
https://github.com/kittinunf/Fuel <br>
https://github.com/cbeust/klaxon <br>
https://github.com/xenomachina/kotlin-argparser <br>

### Build
```bash
gradle clean build
```

### Run 
```bash
java -jar javaclasses-1.0.jar --config PATH_TO_TOKEN -v -N 10
```
```$PATH_TO_TOKEN```
is a file containing OAuth token for GitHub communication (s. https://github.com/settings/tokens)
