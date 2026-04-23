import os
import re
import sys

def extract_strings(directory):
    ui_strings = []
    
    ui_patterns = [
        (r'Text\(\s*"([^"]+)"', 'label'),
        (r'text\s*=\s*"([^"]+)"', 'label'),
        (r'label\s*=\s*"([^"]+)"', 'label'),
        (r'title\s*=\s*"([^"]+)"', 'title'),
        (r'contentDescription\s*=\s*"([^"]+)"', 'desc'),
        (r'showToast\(\s*"([^"]+)"', 'toast'),
        (r'message\s*=\s*"([^"]+)"', 'msg'),
        (r'placeholder\s*=\s*\{\s*Text\(\s*"([^"]+)"', 'hint'),
        (r'hint\s*=\s*"([^"]+)"', 'hint'),
        (r'confirmButton\s*=\s*"([^"]+)"', 'btn'),
        (r'dismissButton\s*=\s*"([^"]+)"', 'btn'),
        (r'subtitle\s*=\s*"([^"]+)"', 'msg'),
        (r'negativeButtonText\s*=\s*"([^"]+)"', 'btn'),
        (r'Toast\.makeText\(.*,\s*"([^"]+)"', 'toast'),
        (r'Snackbar\.make\(.*,\s*"([^"]+)"', 'msg'),
        (r'getString\(\s*"([^"]+)"', 'label'),
    ]
    
    compiled_patterns = [(re.compile(p), prefix) for p, prefix in ui_patterns]
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.kt') or file.endswith('.java'):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                        for i, line in enumerate(lines):
                            if 'Log.' in line or 'println(' in line:
                                continue
                            for pattern, prefix in compiled_patterns:
                                matches = pattern.finditer(line)
                                for match in matches:
                                    string_val = match.group(1)
                                    if string_val and not string_val.startswith('http') and len(string_val.strip()) > 1:
                                        if re.match(r'^[ \t\n\r|.,:;!?-]+$', string_val):
                                            continue
                                        # Filter out technical names
                                        if '_' in string_val and string_val.islower():
                                            continue
                                            
                                        ui_strings.append({
                                            'file': path,
                                            'line': i + 1,
                                            'original': string_val,
                                            'prefix': prefix,
                                            'context': line.strip()
                                        })
                except:
                    pass
                                    
    return ui_strings

def generate_key(text, prefix):
    # Specialized naming for common buttons
    common_buttons = {
        'save': 'btn_save',
        'cancel': 'btn_cancel',
        'done': 'btn_done',
        'delete': 'btn_delete',
        'edit': 'btn_edit',
        'apply': 'btn_apply',
        'clear': 'btn_clear',
        'back': 'btn_back',
        'next': 'btn_next',
        'skip': 'btn_skip',
    }
    
    clean_text = text.lower().strip()
    if prefix == 'btn' and clean_text in common_buttons:
        return common_buttons[clean_text]
        
    key = clean_text
    key = re.sub(r'\$\{[^}]+\}', 'val', key)
    key = re.sub(r'\$[a-zA-Z0-9_]+', 'val', key)
    key = re.sub(r'[^a-z0-9\s]', '', key)
    key = re.sub(r'\s+', '_', key.strip())
    
    if len(key) > 30:
        key = key[:30].rstrip('_')
    if not key:
        key = "string"
        
    return f"{prefix}_{key}"

def convert_to_formatted(text):
    count = 1
    new_text = text
    placeholders = re.findall(r'\$\{[^}]+\}|\$[a-zA-Z0-9_]+', text)
    for p in placeholders:
        new_text = new_text.replace(p, f'%{count}$s', 1)
        count += 1
    return new_text

def process_strings(ui_strings):
    unique_strings = {}
    refactoring = []
    keys_used = {} # key -> original
    
    for item in ui_strings:
        val = item['original']
        prefix = item['prefix']
        context = item['context'].lower()
        
        # Refine prefix
        if 'toast' in context:
            prefix = 'toast'
        elif 'error' in context or 'failed' in context or 'unable' in context:
            prefix = 'msg_error'
        elif 'success' in context or 'saved' in context:
            prefix = 'msg_success'
        elif 'dialog' in context or 'alert' in context:
            prefix = 'dialog'
        
        if val not in unique_strings:
            key = generate_key(val, prefix)
            
            # Handle key collisions
            if key in keys_used and keys_used[key] != val:
                suffix = 1
                new_key = f"{key}_{suffix}"
                while new_key in keys_used and keys_used[new_key] != val:
                    suffix += 1
                    new_key = f"{key}_{suffix}"
                key = new_key
                
            unique_strings[val] = key
            keys_used[key] = val
        
        refactoring.append({
            'file': item['file'],
            'line': item['line'],
            'original': val,
            'key': unique_strings[val]
        })
    
    return unique_strings, refactoring

def main():
    base_path = r'c:\Users\mkn00\AndroidStudioProjects\ExpenseTracker\app\src\main\java'
    ui_strings = extract_strings(base_path)
    unique_strings, refactoring = process_strings(ui_strings)
    
    # Categorize
    categories = {
        'General': [],
        'Authentication': [],
        'Transactions': [],
        'Settings': [],
        'Errors': [],
        'Buttons': [],
        'Dialogs': [],
        'Labels': []
    }
    
    for val, key in unique_strings.items():
        formatted_val = convert_to_formatted(val)
        escaped_val = formatted_val.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace("'", "\\'").replace('"', '\\"')
        entry = (key, escaped_val)
        
        if 'lock' in key or 'biometric' in key or 'pin' in key:
            categories['Authentication'].append(entry)
        elif any(x in key for x in ['transaction', 'amount', 'category', 'spending', 'budget', 'recurring', 'income', 'expense']):
            categories['Transactions'].append(entry)
        elif any(x in key for x in ['setting', 'preference', 'theme', 'profile', 'data_management', 'notification']):
            categories['Settings'].append(entry)
        elif key.startswith('msg_error') or 'failed' in key or 'unable' in key:
            categories['Errors'].append(entry)
        elif key.startswith('btn'):
            categories['Buttons'].append(entry)
        elif key.startswith('dialog'):
            categories['Dialogs'].append(entry)
        elif key.startswith('label') or key.startswith('title') or key.startswith('desc'):
            categories['Labels'].append(entry)
        else:
            categories['General'].append(entry)
            
    # Final Output
    with open('strings_final.xml', 'w', encoding='utf-8') as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n')
        f.write('<resources>\n')
        for cat in ['General', 'Authentication', 'Transactions', 'Settings', 'Errors', 'Buttons', 'Dialogs', 'Labels']:
            entries = categories[cat]
            if entries:
                f.write(f'\n    <!-- {cat} -->\n')
                for key, val in sorted(entries):
                    f.write(f'    <string name="{key}">{val}</string>\n')
        f.write('</resources>\n')
    
    with open('refactoring_final.txt', 'w', encoding='utf-8') as f:
        refactoring.sort(key=lambda x: (x['file'], x['line']))
        for item in refactoring:
            rel_path = os.path.relpath(item['file'], base_path)
            f.write(f"{rel_path}:{item['line']}\n")
            f.write(f'"{item["original"]}" \u2192 R.string.{item["key"]}\n\n')

if __name__ == "__main__":
    main()
