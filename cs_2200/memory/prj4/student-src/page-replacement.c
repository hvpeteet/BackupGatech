#include <stdlib.h>

#include "types.h"
#include "pagetable.h"
#include "global.h"
#include "process.h"

/*******************************************************************************
 * Finds a free physical frame. If none are available, uses a clock sweep
 * algorithm to find a used frame for eviction.
 *
 * @return The physical frame number of a free (or evictable) frame.
 */
pfn_t get_free_frame(void) {
   int i;
   static int old_i = 0;

   /* See if there are any free frames */
   for (i = 0; i < CPU_NUM_FRAMES; i++) {
      if (rlt[i].pcb == NULL) {
         return i;
      }
   }

   /* FIX ME : Problem 5 */
   /* IMPLEMENT A CLOCK SWEEP ALGORITHM HERE */
   /* Note: Think of what kinds of frames can you return before you decide
      to evit one of the pages using the clock sweep and return that frame */
   i = old_i;

   while (1) {
      pte_t* frames_page_table = rlt[i].pcb->pagetable;
      vpn_t frames_vpn = rlt[i].vpn;
      pte_t* frames_pte = &(frames_page_table[frames_vpn]);
      if (frames_pte->used) {
         frames_pte->used = 0;
      } else {
         old_i = i + 1;
         return i;
      }
      i++;
      if (i >= PAGE_TABLE_LEN) {
         i = 0;
      }
   }

}
