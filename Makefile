
SCALA_FILES=$(addprefix ../, $(wildcard src/mypod/*.scala))

all: compile run

compile:
	mkdir -p build/classes
	cd src;scalac -d ../build/classes $(SCALA_FILES)
	
run:
	cd build; scala -classpath classes mypod.Main