# For interactive display, set a wide Qt window:
set terminal qt size 2400,700 font "arial,14"

set style fill solid 1.00 noborder

unset key

set ylabel "Average Response Time [ms]"
set yrange [ 0.0 : 520.0 ] noreverse writeback
set grid ytics lt 0 lw 0.5 lc rgb "#cccccc"

set boxwidth 0.8 absolute
set style data boxes

set xtics ( \
  "JdbcTemplate\nCartesian Product" 0, \
  "Spring Data JDBC\nN+1" 1, \
  "jOOQ\nMULTISET" 2, \
  "Blaze Persistence\nMULTISET" 3, \
  "JPA\nMultiple Queries" 4, \
  "Spring Data JDBC\nMultiple Queries" 5, \
  "jOOQ\nMultiple Queries" 6, \
  "JdbcTemplate\nMultiple Queries" 7 \
) font ",14"
set xrange [-0.6:7.6]
set bmargin 6

# Bars sorted by value descending (left to right)
plot \
  'bbar1.dat' using 1:2 notitle lc rgb "#1565C0", \
  'bbar2.dat' using 1:2 notitle lc rgb "#FF8F00", \
  'bbar3.dat' using 1:2 notitle lc rgb "#2E7D32", \
  'bbar4.dat' using 1:2 notitle lc rgb "#AB47BC", \
  'bbar5.dat' using 1:2 notitle lc rgb "#C62828", \
  'bbar6.dat' using 1:2 notitle lc rgb "#F4511E", \
  'bbar7.dat' using 1:2 notitle lc rgb "#43A047", \
  'bbar8.dat' using 1:2 notitle lc rgb "#64B5F6"
