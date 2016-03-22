
SCALA_FILES=$(addprefix ../, $(wildcard src/local2pod/*.scala)) \
			$(addprefix ../, $(wildcard src/local2pod/mypod/*.scala))

all: compile run

compile:
	mkdir -p build/classes
	cd src;scalac -d ../build/classes $(SCALA_FILES)
	
run:
	cd build; scala -classpath classes local2pod.Main