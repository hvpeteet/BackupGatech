/* CS 2200 - Project 5 - Fall 2015
 * Name - Henry Peteet
 * GTID - hpeteet3
 */

#include "cachesim.h"

// ==============================
// FAKE HEADER
// ==============================

typedef uint64_t custom_time_t;
typedef unsigned char boolean;

typedef struct CACHE_BLOCK_T {
    custom_time_t timestamp;
    boolean valid;
    boolean dirty;
    uint64_t tag;
} cache_block_t;

typedef struct CACHE_LINE_T {
    cache_block_t* blocks;
} cache_line_t;

uint64_t get_index(uint64_t addr);
uint64_t get_tag(uint64_t addr);
void load_into_block(uint64_t addr, cache_block_t* block, boolean write);
custom_time_t clock_time();
cache_block_t* get_hit_block(cache_line_t* line, uint64_t addr);
cache_block_t* get_empty_block(cache_line_t* line);
cache_block_t* get_lru_block(cache_line_t* line);
double calc_emat(struct cache_stats_t* stats);

// ==============================
// END FAKE HEADER
// ==============================

cache_line_t* cache;
uint64_t num_lines;
uint64_t num_blocks_per_line;

int offset_size;
int index_size;
int tag_size;

custom_time_t clock_time() {
    static custom_time_t time = 0;
    return time++;
}

void load_into_block(uint64_t addr, cache_block_t* block, boolean write) {
    block -> tag = get_tag(addr);
    block -> valid = 1;
    block -> dirty = write;
    block -> timestamp = clock_time();
}

uint64_t get_tag(uint64_t addr) {
    // fprintf(stderr, "getting tag\n");
    return addr >> (index_size + offset_size);
}

uint64_t get_index(uint64_t addr) {
    if (tag_size == 64) {
        return 0;
    }
    return ((addr << tag_size) >> tag_size) >> offset_size;
}

cache_block_t* get_hit_block(cache_line_t* line, uint64_t addr) {

    // fprintf(stderr, "getting hit block\n");
    cache_block_t* block = NULL;
    // fprintf(stderr, "\nchecking blocks of line %p\n", line);
    for (int i = 0; i < num_blocks_per_line; i++) {
        // fprintf(stderr, "%d, ", i);
        if (line -> blocks[i].valid && line -> blocks[i].tag == get_tag(addr)) {
            block = &(line -> blocks[i]);
            break;
        }
    }
    return block;
}

cache_block_t* get_empty_block(cache_line_t* line) {
    cache_block_t* block = NULL;
    for (int i = 0; i < num_blocks_per_line; i++) {
        if (line -> blocks[i].valid == 0) {
            block = &(line -> blocks[i]);
            break;
        }
    }
    return block;
}

cache_block_t* get_lru_block(cache_line_t* line) {
    cache_block_t* block = &(line -> blocks[0]);
    custom_time_t min_time = block -> timestamp;
    for (int i = 1; i < num_blocks_per_line; i++) {
        if (line -> blocks[i].timestamp < min_time) {
            block = &(line -> blocks[i]);
            min_time = line -> blocks[i].timestamp;
        }
    }
    return block;
}

double calc_emat(struct cache_stats_t* stats) {
    double hit_time = stats -> access_time;
    double miss_rate = stats -> miss_rate;
    double miss_penalty = stats -> miss_penalty;
    return hit_time + miss_rate * miss_penalty;
}

/**
 * Sub-routine for initializing your cache with the parameters.
 * You may initialize any global variables here.
 *
 * @param C The total size of your cache is 2^C bytes
 * @param S The set associativity is 2^S
 * @param B The size of your block is 2^B bytes
 */
void cache_init(uint64_t C, uint64_t S, uint64_t B) {
    offset_size = B;
    index_size = C - B - S;
    tag_size = 64 - index_size - offset_size;

    num_lines = 1 << index_size;
    num_blocks_per_line = 1 << S;

    // fprintf(stderr, "offset_size: %d\n", offset_size);
    // fprintf(stderr, "index_size: %d\n", index_size);
    // fprintf(stderr, "tag_size: %d\n", tag_size);
    // fprintf(stderr, "num_lines: %d\n", (int) num_lines);
    // fprintf(stderr, "num_blocks_per_line: %d\n", (int) num_blocks_per_line);

    // Allocate all lines in the cache.
    cache = calloc(num_lines, sizeof(cache_line_t));
    if (cache == NULL) {
        printf("\nmalloc failed: out of memory\n");
        return;
    }

    // Allocate all blocks in a line.
    for (int i = 0; i < num_lines; i++) {
        cache[i].blocks = calloc(num_blocks_per_line, sizeof(cache_block_t));
        if (cache[i].blocks == NULL) {
            printf("\nmalloc failed: out of memory\n");
            return;
        }
    }
}

/**
 * Subroutine that simulates one cache event at a time.
 * @param rw The type of access, READ or WRITE
 * @param address The address that is being accessed
 * @param stats The struct that you are supposed to store the stats in
 */
void cache_access (char rw, uint64_t address, struct cache_stats_t *stats) {
    // fprintf(stderr, "\n\n\nstarting cache access\n");
    stats -> accesses++;
    if (rw == READ) {
        stats -> reads++;
    } else {
        stats -> writes++;
    }
    // fprintf(stderr, "getting line");
    // Get the line
    cache_line_t* line = &(cache[get_index(address)]);
    // fprintf(stderr, "got line\n");
    // Search all blocks for a hit.
    // fprintf(stderr, "checking for hit, cache: %p, line: %p\n, index: %p", cache, line, (void*) get_index(address));
    cache_block_t* block = get_hit_block(line, address);
    // fprintf(stderr, "got hit block\n");
    // fprintf(stderr, "block found\n");
    if (block != NULL) {
        // Hit
        // fprintf(stderr, "hit starting\n");
        block -> dirty = (block -> dirty || rw == WRITE);
        block -> timestamp = clock_time();
    } else {
        // fprintf(stderr, "miss starting\n");
        stats -> misses++;
        if (rw == READ) {
            stats -> read_misses++;
        } else {
            stats -> write_misses++;
        }
        // Check for empty.
        block = get_empty_block(line);
        if (block != NULL) {
        } else {
            // Evict LRU.
            block = get_lru_block(line);
            if (block -> dirty) {
                stats -> write_backs++;
            }
        }
        load_into_block(address, block, rw == WRITE);
    }
    // fprintf(stderr, "done with cache access\n");
}

/**
 * Subroutine for cleaning up memory operations and doing any calculations
 * Make sure to free malloced memory here.
 *
 */
void cache_cleanup (struct cache_stats_t *stats) {
    for (int i = 0; i < num_lines; i++) {
        free(cache[i].blocks);
    }
    free(cache);

    stats -> miss_rate = ((double) stats -> misses) / ((double) (stats -> accesses));
    stats -> avg_access_time = calc_emat(stats);
}
