/**************************************
****    DO NOT MODIFY THIS FILE    ****
***************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <unistd.h>
#include "cachesim.h"

#define MAX_SIZE (1<<15)

void print_help_and_exit(void) {
    printf("cachesim [OPTIONS] < traces/file.trace\n");
    printf("  -c C\t\tTotal size in bytes is 2^C\n");
    printf("  -b B\t\tSize of each block in bytes is 2^B\n");
    printf("  -s S\t\tNumber of blocks per set is 2^S\n");
	printf("  -h\t\tThis helpful output\n");
    exit(0);
}

void print_statistics(struct cache_stats_t* p_stats);
uint64_t calculate_cache_size(int c, int b, int s);
void singleTest(uint64_t c, uint64_t b, uint64_t s, char* file_name);


int main(int argc, char* argv[]) {

    char* file_name = argv[1];

    printf("Reading from file %s\n\n", file_name);

    printf("C, B, S, TOTAL, Miss rate, AAT, Read Misses, Write Misses\n");
    // printf("Max Size: %d\n", MAX_SIZE);
    // singleTest(DEFAULT_C, DEFAULT_B, DEFAULT_S, file_name);

    // printf("cache_size(12,3,0) : %lld\n", calculate_cache_size(12, 3, 0));
    // singleTest(12, 3, 0, file_name);
    for (int c = 1; c <= 14; c++) {
        for (int b = 0; b <= 6; b++) {
            for (int s = 0; s <= c - b; s++) {
                if (calculate_cache_size(c, b, s) <= MAX_SIZE) {
                    // fprintf(stderr, "trying c: %d, b: %d, s: %d\n", c, b, s);
                    singleTest(c, b, s, file_name);
                    // singleTest(DEFAULT_C, DEFAULT_B, DEFAULT_S, file_name);
                }
            }
        }
    }
    return 0;
}

uint64_t calculate_cache_size(int c, int b, int s) {
    int offset_size = b;
    int index_size = c - b - s;
    int tag_size = 64 - index_size - offset_size;
    int num_lines = 1 << index_size;
    int num_blocks_per_line = 1 << s;

    // (1 dirty bit + 1 valid bit + tag bits) * number of blocks
    uint64_t metadata_bits_per_line = (2 + tag_size) * num_blocks_per_line;

    uint64_t metadata_bits = num_lines * metadata_bits_per_line;
    uint64_t metadata_bytes = 0;
    if ((metadata_bits % 8) == 0) {
        metadata_bytes = metadata_bits / 8;
    } else {
        metadata_bytes = (metadata_bits / 8) + 1;
    }
    int mem_bytes = (1 << c);
    uint64_t num_bytes = mem_bytes + metadata_bytes;
    return num_bytes;
}

void singleTest(uint64_t c, uint64_t b, uint64_t s, char* file_name) {

    FILE* fin = fopen(file_name, "r");

    /* Setup the cache */
    cache_init(c, s, b);

    /* Setup statistics */
    struct cache_stats_t stats;
    memset(&stats, 0, sizeof(struct cache_stats_t));
    stats.miss_penalty = 100;
    stats.access_time = 2;

    /* Begin reading the file */
    char rw;
    uint64_t address;
    while (!feof(fin)) {
        int ret = fscanf(fin, "%c %" PRIx64 "\n", &rw, &address);
        if(ret == 2) {
            cache_access(rw, address, &stats);
        }
    }

    /* Make sure to free up memory here */
    cache_cleanup(&stats);
    printf("%lld, %lld, %lld, %lld, %lf, %lf, %lld, %lld\n", c, b, s, calculate_cache_size(c, b, s), stats.miss_rate, stats.avg_access_time, stats.read_misses, stats.write_misses);
    fclose(fin);
    // print_statistics(&stats);

}

void print_statistics(struct cache_stats_t* p_stats) {
    printf("Cache Statistics\n");
    printf("Accesses: %" PRIu64 "\n", p_stats->accesses);
    printf("Reads: %" PRIu64 "\n", p_stats->reads);
    printf("Read misses: %" PRIu64 "\n", p_stats->read_misses);
    printf("Writes: %" PRIu64 "\n", p_stats->writes);
    printf("Write misses: %" PRIu64 "\n", p_stats->write_misses);
    printf("Misses: %" PRIu64 "\n", p_stats->misses);
    printf("Writebacks: %" PRIu64 "\n", p_stats->write_backs);
    printf("Access Time: %" PRIu64 "\n", p_stats->access_time);
    printf("Miss Penalty: %" PRIu64 "\n", p_stats->miss_penalty);
    printf("Miss rate: %f\n", p_stats->miss_rate);
    printf("Average access time (AAT): %f\n", p_stats->avg_access_time);
}
