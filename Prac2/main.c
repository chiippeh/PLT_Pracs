#include "llist.h"
#include "intstack.h"
#include <stdio.h>

struct point
{
    int x;
    int y;
};

static void append_point(llist *l, int x, int y)
{
    struct point p = {x, y};
    llist_append(l, &p);
}

static int x_less_than_two(void *raw)
{
    struct point *p = raw;
    if (p->x < 2)
    {
        return 1;
    }
    return 0;
}

static int natural(void *raw)
{
    struct point *p = raw;
    if (p->x > 0 && p->y > 0)
    {
        return 1;
    }
    return 0;
}

int main(void)
{
    llist *l = llist_create(sizeof(struct point));
    append_point(l, 3, 5);
    append_point(l, 2, 6);
    append_point(l, 1, 7);
    append_point(l, 4, 4);
    append_point(l, 0, 8);
    lnode_debug_print(l->head);
    llist_find_all(l,natural);
    LLIST_ITER(l, struct point, v,
               {
                   printf("Point is: %d, %d\n", v.x, v.y);
               })
    llist* findAllNat = llist_find_all(l,natural);
    lnode *n = findAllNat->head;
    while (n)
    {
        struct point *pointer = n->value;
        if (pointer)
        {
            printf("Natural: %d, %d\n", pointer->x, pointer->y);
        }
        n = n->next;
    }
    llist_destroy(findAllNat);
    lnode *found = llist_find(l, x_less_than_two);
    struct point *pt = found->value;
    if (pt)
    {
        printf("Found this node: %d, %d\n", pt->x, pt->y);
        llist_remove(l, found);
        LLIST_ITER(l, struct point, v,
                   {
                       printf("After removal, point is: %d, %d\n", v.x, v.y);
                   })
    }
    lnode *find = llist_find(l, natural);
    struct point *pot = find->value;
    if (pot)
    {
        printf("Found first natural: %d, %d\n", pot->x, pot->y);
    }
    while (l->head)
    {
        pt = l->head->value;
        printf("Removing: %d, %d.  NEXT=%p\n", pt->x, pt->y, (void *)l->head->next);
        llist_remove(l, l->head);
    }
   
    llist_destroy(l);
    intstack *s = intstack_create();
    intstack_push(s, 95);
    intstack_push(s, 20);
    intstack_push(s, 7432);
    int v = 0;
    intstack_pop(s, NULL);
    intstack_pop(s, &v);
    printf("Popped from the stack: %d\n", v);
    intstack_pop(s, &v);
    printf("Popped from the stack: %d\n", v);
    return 0;
}
