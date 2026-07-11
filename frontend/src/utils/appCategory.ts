const categoryLabels: Record<string, { zh: string; en: string }> = {
  'book': { zh: '图书', en: 'Books' },
  'books': { zh: '图书', en: 'Books' },
  'business': { zh: '商务', en: 'Business' },
  'comics': { zh: '漫画', en: 'Comics' },
  'communication': { zh: '通讯', en: 'Communication' },
  'dating': { zh: '约会', en: 'Dating' },
  'education': { zh: '教育', en: 'Education' },
  'educational': { zh: '教育', en: 'Education' },
  'entertainment': { zh: '娱乐', en: 'Entertainment' },
  'events': { zh: '活动', en: 'Events' },
  'finance': { zh: '财务', en: 'Finance' },
  'food & drink': { zh: '美食佳饮', en: 'Food & Drink' },
  'food and drink': { zh: '美食佳饮', en: 'Food & Drink' },
  'games': { zh: '游戏', en: 'Games' },
  'health & fitness': { zh: '健康健美', en: 'Health & Fitness' },
  'health and fitness': { zh: '健康健美', en: 'Health & Fitness' },
  'house & home': { zh: '家居', en: 'House & Home' },
  'kids': { zh: '儿童', en: 'Kids' },
  'libraries & demo': { zh: '库与演示', en: 'Libraries & Demo' },
  'lifestyle': { zh: '生活', en: 'Lifestyle' },
  'magazines & newspapers': { zh: '杂志与报纸', en: 'Magazines & Newspapers' },
  'maps & navigation': { zh: '地图与导航', en: 'Maps & Navigation' },
  'medical': { zh: '医疗', en: 'Medical' },
  'music': { zh: '音乐', en: 'Music' },
  'navigation': { zh: '导航', en: 'Navigation' },
  'news': { zh: '新闻', en: 'News' },
  'parenting': { zh: '育儿', en: 'Parenting' },
  'photo & video': { zh: '摄影与录像', en: 'Photo & Video' },
  'photography': { zh: '摄影', en: 'Photography' },
  'productivity': { zh: '效率', en: 'Productivity' },
  'reference': { zh: '参考', en: 'Reference' },
  'shopping': { zh: '购物', en: 'Shopping' },
  'social': { zh: '社交', en: 'Social' },
  'social networking': { zh: '社交', en: 'Social Networking' },
  'sports': { zh: '体育', en: 'Sports' },
  'strategy': { zh: '策略', en: 'Strategy' },
  'tools': { zh: '工具', en: 'Tools' },
  'travel': { zh: '旅游', en: 'Travel' },
  'utilities': { zh: '工具', en: 'Utilities' },
  'video players & editors': { zh: '视频播放与编辑', en: 'Video Players & Editors' },
  'weather': { zh: '天气', en: 'Weather' },
  'word': { zh: '文字', en: 'Word' },
  '动作': { zh: '动作', en: 'Action' },
  '商务': { zh: '商务', en: 'Business' },
  '财务': { zh: '财务', en: 'Finance' },
  '参考': { zh: '参考', en: 'Reference' },
  '导航': { zh: '导航', en: 'Navigation' },
  '工具': { zh: '工具', en: 'Tools' },
  '购物': { zh: '购物', en: 'Shopping' },
  '健康健美': { zh: '健康健美', en: 'Health & Fitness' },
  '教育': { zh: '教育', en: 'Education' },
  '旅游': { zh: '旅游', en: 'Travel' },
  '美食佳饮': { zh: '美食佳饮', en: 'Food & Drink' },
  '摄影与录像': { zh: '摄影与录像', en: 'Photo & Video' },
  '生活': { zh: '生活', en: 'Lifestyle' },
  '社交': { zh: '社交', en: 'Social Networking' },
  '体育': { zh: '体育', en: 'Sports' },
  '图书': { zh: '图书', en: 'Books' },
  '效率': { zh: '效率', en: 'Productivity' },
  '新闻': { zh: '新闻', en: 'News' },
  '医疗': { zh: '医疗', en: 'Medical' },
  '音乐': { zh: '音乐', en: 'Music' },
  '游戏': { zh: '游戏', en: 'Games' },
  '娱乐': { zh: '娱乐', en: 'Entertainment' }
}

export function formatAppCategory(category: string | null | undefined, locale: string) {
  const value = category?.trim()
  if (!value) {
    return '-'
  }
  const labels = categoryLabels[value.toLowerCase()] || categoryLabels[value]
  if (!labels) {
    return value
  }
  return locale.startsWith('zh') ? labels.zh : labels.en
}
