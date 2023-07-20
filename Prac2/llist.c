#include <stdlib.h>
#include <string.h>
#include "llist.h"
#include <stdio.h>

llist *llist_create(unsigned int elementSize)
{
    llist *l = malloc(sizeof(llist));
    l->elemSize = elementSize;
    l->head = l->last = NULL;
    return l;
}

void llist_append(llist *l, void *data)
{
    lnode *n = malloc(sizeof(lnode));
    n->value = malloc(l->elemSize);
    memcpy(n->value, data, l->elemSize);
    n->next = NULL;
    if (l->head == NULL)
    {
        n->prev = NULL;
        l->head = l->last = n;
    }
    else
    {
        l->last->next = n;
        n->prev = l->last;
        l->last = n;
    }
}

lnode *llist_find(llist *l, int (*f)(void *))
{
    lnode *n = l->head;
    while (n)
    {
        if (f(n->value))
        {
            return n;
        }
        n = n->next;
    }
    return NULL;
}

void llist_remove(llist *l, lnode *n)
{
    if (n->prev)
    {
        n->prev->next = n->next;
    }
    else
    {
        l->head = n->next;
    }
    if (n->next)
    {
        n->next->prev = n->prev;
    }
    else
    {
        l->last = n->prev;
    }
    free(n->value);
    free(n);
}

void llist_destroy(llist *l)
{
    while (l->head)
    {
        llist_remove(l, l->head);
    }
    free(l);
}

void lnode_debug_print(lnode* n){
    printf("Next: %x, Prev: %x, Value: %p\n", n->next, n->prev, n->value);
}

llist* llist_find_all(llist* l, int (*f)(void*)){
   llist* newList = llist_create(l->elemSize);
   lnode *n = l->head;
    while (n){
        if (f(n->value))
        {
            llist_append(newList,n->value);
        }
        n = n->next;
    }
    return newList;
}