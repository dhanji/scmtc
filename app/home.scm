; schematic example request handler

(define get
  (lambda (request response)
    (begin
      (template:mvel response (record (list "name" "Dhanji"))))))