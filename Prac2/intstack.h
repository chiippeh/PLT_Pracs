#include "llist.h"
typedef struct list intstack;
intstack* intstack_create();
void intstack_push(intstack*, int);
int intstack_pop(intstack*, int*);