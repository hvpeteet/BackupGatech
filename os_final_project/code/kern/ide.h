/* ide.c */
bool    ide_probe_disk1(void);
void    ide_set_disk(int diskno);
void    ide_set_partition(uint32_t first_sect, uint32_t nsect);
int ide_read(uint32_t secno, void *dst, size_t nsecs);
int ide_write(uint32_t secno, const void *src, size_t nsecs);
