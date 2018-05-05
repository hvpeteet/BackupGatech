Your compiler must support running with the following command:

java -jar compiler.jar —-runil test.tgr


You can then redirect stdin and stdout.


For ever .tgr file, the test script runTests.sh calls 

java -jar compiler.jar —-runil test.tgr < test_in > test_res

The expected output is contained in file file test_out, which is provided

The test script does a diff of test_res and test_out, and if they are the same outputs pass. Otherwise it outputs fail.


Make sure your submission works with runTests.sh if you want to get full credit.