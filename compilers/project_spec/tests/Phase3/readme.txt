Your compiler must support running with the following command:

java -jar compiler.jar —-runil test.tgr


You can then redirect stdin and stdout as follows: (test_in is the file used as input and test_out is the file used as output)

java -jar compiler.jar —-runil test.tgr < test_in > test_out

That's how the reference test programs, reference inputs, and reference outputs work. It's also how I'll be grading your intermediate code generators/interpreters.

So make sure it works!