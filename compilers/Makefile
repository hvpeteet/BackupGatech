all:
	./gradlew jar
	cp build/libs/4240-1.0.jar compiler.jar

clean:
	gradle clean
	@(rm compiler.jar || true) &> /dev/null
	@(rm project_spec/parser.jar || true) &> /dev/null
	@(rm project_spec/tests/Phase3/compiler.jar || true) &> /dev/null

part-2-tests: all
	cd project_spec/tests && ./runtests.sh

submit: clean
	tar -zcvf submission.tar.gz .

part-3-tests: all
	cp compiler.jar project_spec/tests/Phase3_again/testing && cd project_spec/tests/Phase3_again/testing && ./runTests.sh