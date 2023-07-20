HEADERS = llist.h stack.h

prac2 : llist.o main.o intstack.o
	clang -Weverything -o $@ $^

%.o : %.c $(HEADERS)
	clang -Weverything -c -o $@ $<