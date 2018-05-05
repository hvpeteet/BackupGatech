#ifndef _GLOBAL_H_
#define _GLOBAL_H_

/*******************************************************************************
 * Make the page_size variable globally accessible.
 */
extern unsigned page_size;

/*******************************************************************************
 * Make the mem_size variable globally accessible.
 */
extern unsigned mem_size;

/*******************************************************************************
 * Make the tlb_size variable globally accessible.
 */
extern unsigned tlb_size;

#define PAGE_TABLE_LEN (mem_size / page_size)
#endif/*_GLOBAL_H_*/
