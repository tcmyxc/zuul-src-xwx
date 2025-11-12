对照 zuul-core v1.3.1，自己手敲了大部分代码。同时删除了一些本人不太使用的代码（如GroovyCompiler、GroovyFileFilter）。

同时搭建了一个demo项目，使用这个库搭建了一个转发的网关。
```bash
# 直连访问
http://127.0.0.1:8080/hello

# 通过网关访问
http://127.0.0.1:8081/hello
```