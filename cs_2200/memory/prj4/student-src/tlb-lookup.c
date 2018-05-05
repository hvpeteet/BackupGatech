#include <stdlib.h>
#include <stdio.h>
#include "tlb.h"
#include "pagetable.h"
#include "global.h" /* for tlb_size */
#include "statistics.h"
#include "process.h"

/*******************************************************************************
 * Looks up an address in the TLB. If no entry is found, attempts to access the
 * current page table via cpu_pagetable_lookup().
 *
 * @param vpn The virtual page number to lookup.
 * @param write If the access is a write, this is 1. Otherwise, it is 0.
 * @return The physical frame number of the page we are accessing.
 */
pfn_t tlb_lookup(vpn_t vpn, int write) {
   pfn_t pfn;
   static int old_i;
   // return pagetable_lookup(vpn, write);

   /*
    * FIX ME : Step 6
    */

   /*
    * Search the TLB for the given VPN. Make sure to increment count_tlbhits if
    * it was a hit!
    */
    for (int i = 0; i < tlb_size; i++) {
      tlbe_t entry = tlb[i];
      if (entry.valid && entry.vpn == vpn && rlt[entry.pfn].pcb->pid == current->pid) {
        // printf("\n==================\n");
        // printf("TLB HIT");
        // printf("\n==================\n");
        count_tlbhits++;
        return entry.pfn;
      }
    }

   /* If it does not exist (it was not a hit), call the page table reader */
   pfn = pagetable_lookup(vpn, write);

   /*
    * Replace an entry in the TLB if we missed. Pick invalid entries first,
    * then do a clock-sweep to find a victim.
    */
    unsigned char done = 0;
    int i = 0;
    tlbe_t* entry;

    for (i = 0; i < tlb_size; i++) {
      entry = &(tlb[i]);
      if (!entry->valid) {
        done = 1;
        break;
      }
    }

    if (!done) {
      i = old_i;
    }
    while (!done) {
      i++;
      if (i >= tlb_size) {
        i = 0;
      }
      entry = &(tlb[i]);
      if (!entry->used) {
        done = 1;
      } else {
        entry->used = 0;
      }
    }
    old_i = i;

   /*
    * Perform TLB house keeping. This means marking the found TLB entry as
    * accessed and if we had a write, dirty. We also need to update the page
    * table in memory with the same data.
    *
    * We'll assume that this write is scheduled and the CPU doesn't actually
    * have to wait for it to finish (there wouldn't be much point to a TLB if
    * we didn't!).
    */
    entry -> valid = 1;
    entry -> dirty = write;
    entry -> used = 1;
    entry -> vpn = vpn;
    entry -> pfn = pfn;
    if (write) {
      current_pagetable[entry->vpn].dirty = 1;
      current_pagetable[entry->vpn].used = 1;
    }
    return pfn;
}

