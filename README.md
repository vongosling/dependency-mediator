Dependency analysis based on Maven, apply to remedy some unexpected exceptions or errors.

As we know,when we are building a web project based on maven POM,we often use maven dependency plugin to solve jar conflict,such as:mvn dependency:tree -Dverbose -Dincludes=commons-collections,
but if we build our project to war package according with Java EE specification.we always have nothing to do but with the naked eye to lookup some underlying conflict jar packages.of course,which 
depend on Java EE container classloader's class loading mechanism.

Now,I write a plugin based on maven to solve this problem,if you have better idea or improving suggestion,plean send to me:fengjia10@gmail.com	