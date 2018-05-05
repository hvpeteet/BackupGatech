#include "swapfile.h"
#include "statistics.h"
#include "pagetable.h"

/*******************************************************************************
 * Looks up an address in the current page table. If the entry for the given
 * page is not valid, increments count_pagefaults and traps to the OS.
 *
 * @param vpn The virtual page number to lookup.
 * @param write If the access is a write, this is 1. Otherwise, it is 0.
 * @return The physical frame number of the page we are accessing.
 */
pfn_t pagetable_lookup(vpn_t vpn, int write) {
    pte_t* entry = current_pagetable + vpn;
    if (!(entry -> valid)) {
        count_pagefaults++;
        pagefault_handler(vpn, write); // don't assign to anything since pagefault_handler updates the entry in the page table for us.
    }
    if (write) {
        entry -> dirty = 1;
    }
    entry -> used = 1;
    pfn_t pfn = entry -> pfn;
    return pfn;
}
