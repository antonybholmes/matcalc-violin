import sys
import libplot
import pandas as pd
import numpy as np
import argparse

COLUMN_WIDTH = 2

def format_axes(ax, args):
    libplot.format_axes(ax, x=args.xlabel, y=args.ylabel)
    ax.tick_params(axis='x', which='minor', bottom=False)

parser = argparse.ArgumentParser()
parser.add_argument('file') 
parser.add_argument('color_file')
parser.add_argument('out')  
parser.add_argument('-x', '--xlabel', type=str, nargs='?', default='Category')
parser.add_argument('-y', '--ylabel', type=str, nargs='?', default='Value')
parser.add_argument('-v', '--violinplot', type=str, nargs='?', default=True)
parser.add_argument('-b', '--boxplot', type=str, nargs='?', default=False)
parser.add_argument('-s', '--swarmplot', type=str, nargs='?', default=False)
args = parser.parse_args()

file = args.file
color_file = args.color_file
out = args.out

violinplot = args.violinplot == 'true'
boxplot = args.boxplot == 'true'
swarmplot = args.swarmplot == 'true'

libplot.setup()

print(args)

df = pd.read_csv(file, sep='\t', header=0)

df_color = pd.read_csv(color_file, sep='\t', header=0)

colors=df_color['Color'].tolist()

w = np.unique(df['Label']).size  * COLUMN_WIDTH

fig = libplot.new_base_fig(w=w, h=6)

if violinplot:
    ax = libplot.new_ax(fig)
    
    if boxplot or swarmplot:
        tint = 0.5
    else:
        tint = 0
        
    libplot.violinplot(df, x='Label', y='Value', colors=colors, width=0.8, tint=tint, ax=ax)

    format_axes(ax, args)

if boxplot:
    if violinplot:
        ax2 = libplot.new_ax(fig, sharex=ax, sharey=ax, zorder=10)
        ax2.patch.set_alpha(0)
    else:
       ax2 = libplot.new_ax(fig)

    if swarmplot:
        tint = 0.8
    else:
        tint = 0
        
    libplot.boxplot(df, x='Label', y='Value', colors=colors, width=0.2, tint=tint, ax=ax2)
    
    if violinplot:
        libplot.invisible_axes(ax2)
    else:
        format_axes(ax2, args)
    

if swarmplot:
    if violinplot or boxplot:
        ax3 = libplot.new_ax(fig, sharex=ax, sharey=ax, zorder=100)
        ax3.patch.set_alpha(0)
    else:
        ax3 = libplot.new_ax(fig)
    
    libplot.swarmplot(df, x='Label', y='Value', colors=colors, ax=ax3)
    
    if violinplot or boxplot:
        libplot.invisible_axes(ax3)
    else:
        format_axes(ax3, args)


libplot.savefig(fig, out)

