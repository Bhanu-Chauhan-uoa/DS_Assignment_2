JAVAC = javac
JAVA = java
SRC = src
CLASS_FILES := out
JACKSON_JAR_DIR = lib/jackson

default: compile

compile:
	$(JAVAC) -cp "$(JACKSON_JAR_DIR)/*" $(SRC)/*.java -d "$(CLASS_FILES)"

.PHONY: test
TESTS := 1 2 3 4 5 6 7

test:
	@for /L %%i in (1,1,7) do ( \
		call test\test%%i.sh && timeout /t 30 \
	)