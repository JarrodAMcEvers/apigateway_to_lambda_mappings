import boto3
import json
methods = ['GET', 'POST', 'DELETE', 'PUT', 'PATCH']

data = dict()
apigateway = boto3.client('apigateway')

for api in apigateway.get_rest_apis()['items']:
    api_data = []
    results = apigateway.get_resources(restApiId=api['id'])
    for result in results['items']:
        for method in methods:
            try:
                integration = apigateway.get_integration(restApiId=api['id'], resourceId=result['id'], httpMethod=method)
                if 'lambda' not in integration['uri']:
                    continue
                lambda_function = integration['uri'].split(':')[-1].split('/')[0]
                api_data.append({
                    'api_resource': '%s %s' % (method, result['path']),
                    'lambda_function': lambda_function
                })
            except apigateway.exceptions.NotFoundException as e:
                continue
    data[api['name']] = api_data

with open('apigateway_lambda.json', 'w') as outfile:
    json.dump(data, outfile)
