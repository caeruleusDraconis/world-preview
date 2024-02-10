#!/usr/bin/python

import json, sys
import matplotlib.pyplot as plt

data = json.loads(sys.argv[1])
bins = [x - 0.5 for x in range(min(data), max(data))]

fig, ax = plt.subplots()
_ = ax.hist(data, bins)
ax.set_xticks([x + 0.5 for x in bins])

plt.show()
