# supernatural-computing
Repository for team SuperNatural's Natural Computing 2019 Project, for the GECCO 2019 Bi-objective Traveling Thief Competition.

WARNING: this program uses a lot of time, threads and memory, especially for the larger problems.

Build and run using the following commands (JDK 1.8) in the `src/main/java/` folder:
```
javac Runner.java
java Runner
```

You might have to supply the extra command line option `-Xms -Xmx -Xss` to give the program more memory e.g.:
```
java -Xms50G -Xmx400G -Xss5G
```

Additional libraries can be added as JAR files by supplying the following arguments to the compile and run commands:
```
-cp {path/to/jar}:.
```