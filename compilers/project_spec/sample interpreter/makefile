JAVAC=javac
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: jar

myProgram: $(classes)

clean :
	rm -f *.class
	rm -f *.jar

%.class : %.java
	$(JAVAC) $<

jar : 
	javac $(sources)
	jar cmf META-INF/MANIFEST.MF ti.jar $(classes)
