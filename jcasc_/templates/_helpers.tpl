{{/* Standard Tracking Annotations */}}
{{- define "jcasc.tracking.annotation" }}
ercot.com/scm: {{ .Values.jcasc.scm | quote }}
{{- end }}

{{/* Shared Basic Labels */}}
{{- define "jcasc.labels" }}
helm.sh/chart: "{{ .Chart.Name }}-{{ .Chart.Version }}"
app.kubernetes.io/name: {{ .Values.jcasc.component | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/component: {{ .Values.jcasc.component | quote }}
{{- end }}

{{/* Secret API Version */}}
{{- define "secret.version" }}
apiVersion: "v1"
{{- end }}

{{/* Service Account API Version */}}
{{- define "svcacct.version" }}
apiVersion: "v1"
{{- end }}

{{/* RoleBinding API Version */}}
{{- define "rolebinding.version" }}
apiVersion: "rbac.authorization.k8s.io/v1"
{{- end }}

{{/* SCC API Version */}}
{{- define "scc.version" }}
apiVersion: "security.openshift.io/v1"
{{- end }}

{{/* PVC API Version */}}
{{- define "pvc.version" }}
apiVersion: "v1"
{{- end }}

{{/* NetworkPolicy API Version */}}
{{- define "np.version" }}
apiVersion: "networking.k8s.io/v1"
{{- end }}