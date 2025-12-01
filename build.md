# Instructions for building and running

## Cleaning

Every once in a while you just need to clean house. To do that run:

```bash
./gradlew clean
```

## Data Generation

When using code generated data we might need to run the data generation process.

```bash
./gradlew runDatagen
```

## Build Jar File

The package we can share is a Jar file. To build that we run:

```bash
./gradlew build
```
This creates the .jar file which is what needs to be shared, along with the Fabric jar. Our jar can be found in build/libs once you run the build sucessfully.

## Run in Debugger

When we are actively developing the code we want to run it through a debugger. That captures a lot of data that the program outputs so we can track down any errors.

```bash
./gradlew --no-daemon runClient
```

To run it with the output that goes to the terminal also going to a file, we can use the "tee" command, like this:

```bash
./gradlew --no-daemon runClient | tee runOutput.log
```

But we shouldn't need that because the debugging info should be in run/logs/latest.log
