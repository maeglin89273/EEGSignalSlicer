import math
import csv
import numpy

AMP = 10
def expand(x):
	return (x, 
			AMP * math.sin(2 * math.pi *x), 
			AMP * math.cos(2 * math.pi *x), 
			AMP * math.sin(2 * math.pi * 30 * x), 
			AMP *(3 * math.cos(2 * math.pi * 10 * x + math.pi / 4)), 
			AMP * (2 * math.cos(2 * math.pi * 10 * x) + math.sin(2 * math.pi * 10 * x)), 
			AMP * (math.sin(2 * math.pi * 8 * x) + math.sin(2 * math.pi * 5 * x)),
			AMP * ((x * 256) % 32) * 0.1 - 16,
			AMP * (int(((x * 256) % 32) > 15) * 2 - 1))

with open("sin_test.txt", "wb") as f:
	csvFile = csv.writer(f)
	for i in xrange(5):
		csvFile.writerow([])
	
	for row in map(expand, numpy.arange(0, 30, 1/256.0)):
		csvFile.writerow(row)
