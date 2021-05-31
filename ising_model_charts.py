from typing import List

import plotly.graph_objects as go


def heatmap_chart(matrix: List[List[int]], L, t_star) -> None:
    import plotly.figure_factory as ff
    txt = [["" for _ in range(len(matrix))] for _ in range(len(matrix))]
    labels = [i + 1 for i in range(len(matrix))]
    tmp = []
    for i in range(len(matrix) - 1, -1, -1):
        tmp.append(matrix[i])
    fig = ff.create_annotated_heatmap(tmp, annotation_text=txt,
                                      colorscale=['rgb(244,244,248)', 'rgb(255,0,0)'],
                                      zmin=-1, zmax=1, showscale=False, x=labels, y=labels[::-1])
    fig.update_xaxes(showline=True, linewidth=1, linecolor='black', mirror=True)
    fig.update_yaxes(showline=True, linewidth=1, linecolor='black', mirror=True)
    fig.update_layout(width=600, height=600,
                      title=f'Configurations of spins L = {L} and T*={t_star}', margin_t=65,
                      title_x=0.5,
                      legend_title_side='top')
    fig.show()


def point_plot(scatter: list, x_title, ytitle) -> None:
    fig = go.Figure()
    for s in scatter:
        fig.add_trace(s)
    fig.update_layout({'plot_bgcolor': 'rgb(255, 255, 255)', 'paper_bgcolor': 'rgb(255, 255, 255)'}, width=600,
                      height=600)
    fig.update_xaxes(showline=True, linewidth=1, linecolor='black', mirror=True, ticks='outside', tickwidth=2,
                     ticklen=8, title=x_title, title_font_size=20, title_font_color='black')
    fig.update_yaxes(showline=True, linewidth=1, linecolor='black', mirror=True, ticks='outside', tickwidth=2,
                     ticklen=8, title=ytitle, title_font_size=20, title_font_color='black')
    fig.show()


def create_scatter_plot(l_defs: List[str], filename: str) -> List:
    scaters = []
    markers = ['triangle-up-open-dot', 'square-open-dot', 'cross-open-dot', 'octagon-open-dot', 'triangle-up-open-dot']
    colors = ['blue', 'green', 'red', 'brown']
    counter = 0
    for l in l_defs:
        with open(filename.format(l), 'r') as open_file:
            x = []
            y = []
            for line in open_file.readlines():
                split = line.split()
                x.append(float(split[0]))
                y.append(float(split[1]))
            df = dict({'x': x, 'y': y, })
            scaters.append(go.Scatter(df, x=x, y=y, mode='markers', name=f"L={l}",
                                      marker=dict(size=5, color=colors[counter],
                                                  symbol=markers[counter])))
            counter += 1
    return scaters


def read_matrix(filNam: str):
    matrix_list = []
    with open(filNam, 'r') as matrixFile:
        for line in matrixFile.readlines():
            line = line.split()
            line = [int(i) for i in line]
            matrix_list.append(line)
    return matrix_list


def example_spin_config():
    for l_size in [8, 16, 35]:
        for t_star in [1.0, 2.26, 10]:
            heatmap_chart(read_matrix(f"config_L={l_size}_T={t_star}.txt"), l_size, t_star)


if __name__ == '__main__':
    example_spin_config()
    point_plot(create_scatter_plot(['8', '16', '36'], "heat_file_L{l}.txt"),
               'Reduced Temperature T*', 'Heat Capacity')
    point_plot(create_scatter_plot(['5', '10', '30', '60'], "magnetization_file_L{l}.txt"),
               'T*', '<m>')
