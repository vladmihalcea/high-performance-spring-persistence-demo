# For interactive display:
set terminal qt size 800,700 font "arial,14"

# Uncomment ONE of the following pairs to render to file instead:
# set terminal svg enhanced font "arial,14" size 800,700
# set output 'BenchmarkResultsBest4.svg'
#
# set terminal pngcairo enhanced font "arial,14" fontscale 1.0 size 800, 700
# set output 'BenchmarkResultsBest4.png'

set style fill solid 1.00 noborder

unset key

set ylabel "Average Response Time [ms]"
set yrange [ 0.0 : 28.0 ] noreverse writeback
set grid ytics lt 0 lw 0.5 lc rgb "#cccccc"
set bmargin 6

set boxwidth 0.4 absolute
set style data boxes

set xtics ( \
  "JPA\nMultiple Queries" 0, \
  "Spring Data JDBC\nMultiple Queries" 1, \
  "jOOQ\nMultiple Queries" 2, \
  "JdbcTemplate\nMultiple Queries" 3 \
) font ",14"
set xrange [-0.6:3.6]

# Best 4 sorted by value descending (left to right)
plot \
  'best1.dat' using 1:2 notitle lc rgb "#C62828", \
  'best2.dat' using 1:2 notitle lc rgb "#F4511E", \
  'best3.dat' using 1:2 notitle lc rgb "#43A047", \
  'best4.dat' using 1:2 notitle lc rgb "#64B5F6"
