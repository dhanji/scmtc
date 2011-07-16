; schematic example request handler

(define get
  (lambda (request response)
    (begin
      (display #{request from #[ (request/user-agent request) ]# }#)
      (newline)
      (template:mvel response "test.mvel")
      (after response (lambda () (display "done"))))))