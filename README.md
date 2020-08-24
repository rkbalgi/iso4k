# iso4k
A library to work with ISO8583 messages built using Kotlin

The goal of this project is to provide a library like JPOS which can then be integrated with other applications like https://github.com/rkbalgi/keedoh or https://github.com/rkbalgi/tcptester 

(Keedoh already implements some of the logic for dealing with ISO8583 messages and the same will be extracted and reused)

The specifications will be defined in yaml files and will be borrowed from my other project (https://github.com/rkbalgi/iso8583_rs/blob/master/sample_spec/sample_spec.yaml)

Glossary -
1. A spec or specification defines message segments and header fields
2. A message segment is a layout of a ISO8583 request or a response
3. The parsed header determines the message segment to be used
4. A transaction a instance of a "message" composed of a request and a response 
