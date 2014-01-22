
echo "set key inside right bottom box 
set datafile separator \";\"
set decimalsign locale
set xlabel 'Trigram similarity'
set ylabel 'F1-measure'
set term pdfcairo enhanced color solid font \"Helvetica,7\" size 12cm,8cm
set output 'bfs_depth_diag.pdf'
set xrange[0.7:1.01]
set grid
set pointsize 1
plot './bfs_depth_diag.csv' using 1:2 title 'trigram-only' with points, './bfs_depth_diag.csv' using 1:3 title 'd = 1' with points lt 7, './bfs_depth_diag.csv' using 1:4 title 'd = 2' with points, './bfs_depth_diag.csv' using 1:5 title 'd = 3' with points pt 8 lt 8
" | gnuplot


echo "set key inside right bottom box 
set datafile separator \";\"
set decimalsign locale
set xlabel 'Trigram similarity'
set ylabel 'F1-measure'
set term pdfcairo enhanced color solid font \"Helvetica,7\" size 12cm,8cm
set output 'surface_forms_diag.pdf'
set xrange[0.49:1.01]
set grid
set pointsize 1
set style line 2 lt 1 lw 1 pt 4 linecolor rgb \"forestgreen\"
plot './surface_forms_diag.csv' using 1:2 title 'with surface forms, d = 2' with points pt 3 lt 3, './surface_forms_diag.csv' using 1:3 title 'without surface forms, d = 2' with points ls 2
" | gnuplot
