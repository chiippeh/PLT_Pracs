#ifndef LLIST_H
#define LLIST_H
#define LLIST_ITER(l, t, varName, body)       \
   {                                          \
      struct node *cur_node = l->head;        \
      while (cur_node)                        \
      {                                       \
         t varName = *(t *)(cur_node->value); \
         body                                 \
             cur_node = cur_node->next;       \
      }                                       \
   }

struct node
{
   struct node *prev;
   struct node *next;
   void *value;
};

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wpadded"
struct list
{
   struct node *head;
   struct node *last;
   unsigned int elemSize;
};
#pragma clang diagnostic pop

typedef struct list llist;
typedef struct node lnode;

llist *llist_create(unsigned int);
void llist_append(llist *, void *);
lnode *llist_find(llist *l, int (*f)(void *));
void llist_remove(llist *, lnode *);
void lnode_debug_print(lnode *);
llist *llist_find_all(llist *l,int (*f)(void*));
#endif