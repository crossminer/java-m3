# java-m3

A simple standalone Rascal tool that can automatically infer M3 models from JARs or Eclipse Java projects.

## Build

```
cd org.eclipse.scava.java.m3
mvn clean package
```

This will produce a standalone JAR with all dependencies included in `org.eclipse.scava.java.m3/target/org.eclipse.scava.java.m3-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.

## Usage

### Build an M3 model from an Eclipse project and extract the method declarations/invocations

```
try {
  M3Java extractor = new M3Java();
  IValue m3 = extractor.createM3FromEclipseProject("SimpleProjectName");

  // Each key in the map is a method declaration
  // The set of values associated to a key is the set of method invocations
  Multimap<String, String> methodInvocations = extractor.extractMethodInvocations(m3);
} catch (Exception e) {
  e.printStackTrace();
}
```

### Build an M3 model from a JAR and extract the method declarations/invocations

```
try {
  M3Java extractor = new M3Java();
  IValue m3 = extractor.createM3FromJar("/full/path/to/Jar.jar");

  // Each key in the map is a method declaration
  // The set of values associated to a key is the set of method invocations
  Multimap<String, String> methodInvocations = extractor.extractMethodInvocations(m3);
} catch (Exception e) {
  e.printStackTrace();
}
```

## Example

The `org.eclipse.scava.java.m3.example` project is a simple Eclipse plug-in project that allows to right-click a Java project in Eclipse to access a menu `M3 -> Generate M3` which will build the M3 model of the project, extract the list of method declarations and invocations, and print the result in a pop-up window.

First, copy the JAR created above in the `lib/` directory:

```
cp org.eclipse.scava.java.m3/target/org.eclipse.scava.java.m3-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.eclipse.scava.java.m3.example/lib
```

Then, import the project into Eclipse, run it in a new Eclipse instance, and access the menu. The example code that uses the `M3Java` extractor is located in https://github.com/crossminer/java-m3/blob/master/org.eclipse.scava.java.m3.example/src/org/eclipse/scava/java/m3/example/popup/actions/GenerateM3Action.java#L45-L53.
