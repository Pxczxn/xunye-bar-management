/**
 * Ant Design 组件的深色主题配置
 */

export const darkSelectProps = {
  className: 'xunye-select',
  classNames: {
    popup: {
      root: 'xunye-select-dropdown',
    },
  },
  styles: {
    root: {
      width: '100%',
      backgroundColor: '#101014',
      border: '1px solid #2A2A31',
    },
    content: {
      color: '#F4EBDD',
    },
    suffix: {
      color: '#AFA79B',
    },
    popup: {
      root: {
        backgroundColor: '#1A1A1F',
        border: '1px solid #2A2A31',
      },
    },
  },
} as const;

export const modalStyles = {
  content: {
    backgroundColor: '#101014',
    border: '1px solid #2A2A31',
  },
  header: {
    backgroundColor: '#101014',
    borderBottom: '1px solid #2A2A31',
  },
  body: {
    backgroundColor: '#101014',
  },
  footer: {
    backgroundColor: '#101014',
    borderTop: '1px solid #2A2A31',
  },
} as const;
