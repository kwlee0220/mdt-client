#!	/bin/bash

java -jar antlr-4.13.2-complete.jar MdtExpr.g4 -o mdt/model/expr -no-listener -visitor
