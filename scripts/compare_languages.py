"""Small synthetic screen, not proof of language support or a native-speaker evaluation."""
import json
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from hospitality.model import OllamaModel, obj, TEXT

model=OllamaModel()
report={'scope':'Exploratory synthetic language screen; no native-speaker validation','results':[]}
for language in ('Spanish','Hindi','Tamil'):
    for source in ('We loved the coffee tasting, but the directions were confusing.',
                   'The short tasting takes 20 minutes. I need to check whether tomorrow is available.'):
        row={'language':language,'source':source}
        try:
            result,metrics=model.generate('Translate the supplied English text faithfully into '+language+'. Preserve numbers, uncertainty and criticism. Return only the requested translation, without additions.',{'source':source},obj(translation=TEXT))
            row.update(result=result,metrics=metrics)
        except Exception as exc: row['error']=str(exc)
        report['results'].append(row)
print(json.dumps(report,ensure_ascii=False,indent=2))
