Use this module to get the top N most popular public java classes in a repository belonging to a certain user.
If there are more than N classes with the same top usage frequency, all of them will be reported.

External libraries: <br>
https://github.com/kittinunf/Fuel <br>
https://github.com/cbeust/klaxon <br>
https://github.com/xenomachina/kotlin-argparser <br>

Build

gradle clean build

Run 

java -jar ./build/libs/javaclasses-1.0.jar --config PATH_TO_TOKEN -v -N 10
