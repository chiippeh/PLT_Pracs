#include "intstack.h"
#include <stdlib.h>
intstack *intstack_create()
{
    return llist_create(sizeof(int));
}
void intstack_push(intstack *s, int v)
{
    llist_append(s, &v);
}
int intstack_pop(intstack *s, int *storage)
{
    if (s->last == NULL)
    {
        return 0;
    }
    if (storage != NULL)
    {
        *storage = *(int *)(s->last->value);
    }
    llist_remove(s, s->last);
    return 1;
}
