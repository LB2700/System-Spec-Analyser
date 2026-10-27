import pygal

fileName = input("Enter the name of the csv file to be converted (DONT ADD .csv TO IT): ")
file = open("output/csvFiles/" + fileName + ".csv", "r") # opens the cache results to be read

# dictionary so each stride gets its own list of times
timesByStride = {}
setSizes = []

# read the csv file
header = file.readline()  # skip the header row since its only used for readability

for line in file:
    content = line.strip()
    parts = content.split(",")

    stride = int(parts[0])
    setSizeKB = int(parts[1])
    nsPerAccess = float(parts[2])

    # create a new list the first time we see a stride
    if stride not in timesByStride:
        timesByStride[stride] = []

    timesByStride[stride].append(nsPerAccess)

    # collect the set sizes once (they repeat for every stride)
    if setSizeKB not in setSizes:
        setSizes.append(setSizeKB)

file.close()

# function to turn a size in KB into a readable label
def sizeLabel(kb):
    if kb >= 1024:
        return str(kb // 1024) + " MB"
    else:
        return str(kb) + " KB"

# create the graph
chart = pygal.Line()
chart.title = "Memory Hierarchy: Time per Access vs Working Set Size"
chart.x_title = "Working set size"
chart.y_title = "Time per access (ns)"
chart.x_labels = list(map(sizeLabel, setSizes))
chart.x_label_rotation = 45

# plot one line per stride
for stride in timesByStride:
    chart.add(str(stride) + " B stride", timesByStride[stride])

fileOutput = input("enter the name of the Output file: ")
chart.render_to_file("output/graphs/" + fileOutput + ".svg") # save the file
print("Graph saved to cache_plot.svg")